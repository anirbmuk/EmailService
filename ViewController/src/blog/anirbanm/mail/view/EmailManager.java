package blog.anirbanm.mail.view;

import java.io.File;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import javax.faces.event.ActionEvent;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import oracle.adf.view.rich.event.DialogEvent;
import oracle.adf.view.rich.event.PopupCanceledEvent;

public class EmailManager {
    
    public EmailManager() {
        super();
    }
    
    public void sendEmail(final DialogEvent dialogEvent) {
        final String from = (String) ADFUtils.getBoundAttributeValue("from");
        final String to = (String) ADFUtils.getBoundAttributeValue("to");
        final String cc = (String) ADFUtils.getBoundAttributeValue("cc");
        final String bcc = (String) ADFUtils.getBoundAttributeValue("bcc");
        final String subject = (String) ADFUtils.getBoundAttributeValue("subject");
        final String text = (String) ADFUtils.getBoundAttributeValue("text");
        final String password = (String) ADFUtils.getBoundAttributeValue("password");

        /**
         * The below properties are specific to GMail.
         * To use this feature for corporate application,
         * you need appropriate smtp host and port details
         */
        Properties properties = System.getProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        
        /**
         * Setting up email service provider authentication
         */
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });
        
        /**
         * Setting up email object
         */
        try {
            /**
             * Email to, from and subject
             */
            final MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            if (cc != null) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
            }
            if (bcc != null) {
                message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));
            }
            message.setSubject(subject);
            
            /**
             * Email body
             */
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(text);
            
            /**
             * Email attachment
             */
            final File attachment = new File("C:\\resources\\attachment.txt");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            if (attachment != null) {
                messageBodyPart = new MimeBodyPart();
                DataSource ds = new FileDataSource(attachment);
                messageBodyPart.setDataHandler(new DataHandler(ds));
                messageBodyPart.setFileName(attachment.getName());
                multipart.addBodyPart(messageBodyPart);
            }
            message.setContent(multipart);
            
            /**
             * Email send action
             */
            Transport.send(message);
            
        } catch (MessagingException me) {
            me.printStackTrace();
        }
    }

    public void cancelSend(final PopupCanceledEvent popupCanceledEvent) {
        ADFUtils.setBoundAttributeValue("password", null);
    }
    
    public void cleanup(final ActionEvent actionEvent) {
        cleanup();
    }
    
    private void cleanup() {
        ADFUtils.setBoundAttributeValue("from", null);
        ADFUtils.setBoundAttributeValue("to", null);
        ADFUtils.setBoundAttributeValue("cc", null);
        ADFUtils.setBoundAttributeValue("bcc", null);
        ADFUtils.setBoundAttributeValue("subject", null);
        ADFUtils.setBoundAttributeValue("text", null);
        ADFUtils.setBoundAttributeValue("password", null);
    }
}
