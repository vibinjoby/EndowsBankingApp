package com.endows.app.helper;

import android.content.Context;
import android.os.AsyncTask;

import com.endows.app.models.app.EmailTemplateDetails;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailHelper extends AsyncTask<String, Void, Void> {
    private Context context;
    private EmailTemplateDetails emailTemplateDetails;

    public EmailHelper(Context context, EmailTemplateDetails emailTemplateDetails) {
        this.context = context;
        this.emailTemplateDetails = emailTemplateDetails;
    }

    @Override
    protected Void doInBackground(String... strArgs) {
        final String username = EncryptPasswords.decrypt("v0wTNKXJtZ9rCnVA7KJezo1j/9PfFYms");
        final String password = EncryptPasswords.decrypt("g+tpRVxNs1LTsXa01bcc4w==");
        Properties prop = new Properties();
        //prop.put("mail.smtp.ssl.trust",  "smtp.gmail.com");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.debug", "true");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(emailTemplateDetails.getSenderEmailId())
            );
            message.setSubject("Secured Email From ENDOWS");
            message.setSentDate(new Date());

            InputStream inStream = context.getResources().getAssets().open(emailTemplateDetails.getTemplateName());
            byte[] b = new byte[inStream.available()];
            inStream.read(b);
            String text = new String(b);

            text = getReplacedText(text);
            message.setContent(text, "text/html");
            System.out.println("Trying to send email...");
            // sends the e-mail
            Transport.send(message);
            System.out.println("Mail sent successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getReplacedText(String text) {
        if (emailTemplateDetails.isCardTemplate()) {
            text = text.replace("Card Number:", "Card Number: " + emailTemplateDetails.getCardDetails().getCardNumber())
                    .replace("secure PIN:", "Pin: " + emailTemplateDetails.getCardDetails().getPinNumber())
                    .replace("Text for Debit.", "");
        } else if (emailTemplateDetails.isOtpTemplate()) {
            text = text.replace("ENTER OTP HERE", "Your verification code is " + emailTemplateDetails.getVerificationCode());
        } else if (emailTemplateDetails.isTransactionTemplate()) {
            String transactionStr = ("Y".equalsIgnoreCase(emailTemplateDetails.getTransactionHistory().getIsCredit())) ? " Credited" : " Debited";
            text = text.replace("From Account Number :", "Card Number :" + emailTemplateDetails.getCardDetails().getCardNumber())
                    .replace("Transaction Amount : (Credit/Debit)", "Transaction Amount : $" + emailTemplateDetails.getTransactionHistory().getAmount() + transactionStr)
                    .replace("Date and time of The transaction :", "Time stamp : " + emailTemplateDetails.getTransactionHistory().getTimestamp());
        } else if (emailTemplateDetails.isBeneficiaryTemplate()) {
            text = text.replace("ACCOUNT_NAME", emailTemplateDetails.getBeneficiaryDetail().getBeneficiaryName())
                    .replace("TIME_STAMP", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
        return text;
    }
}
