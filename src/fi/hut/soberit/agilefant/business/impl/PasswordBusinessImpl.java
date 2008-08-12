package fi.hut.soberit.agilefant.business.impl;

import java.util.Date;
import java.util.Random;

import org.antlr.stringtemplate.StringTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.providers.encoding.PasswordEncoder;

import fi.hut.soberit.agilefant.business.PasswordBusiness;
import fi.hut.soberit.agilefant.db.UserDAO;
import fi.hut.soberit.agilefant.model.User;


public class PasswordBusinessImpl implements PasswordBusiness {
    private JavaMailSender mailSender;

    private SimpleMailMessage newPasswordTemplate;

    private UserDAO userDAO;

    private PasswordEncoder passwordEncoder;

    public void generateAndMailPassword(int user_id) {
        User user = userDAO.get(user_id);
        String password = generateNewPassword();
        user.setPassword(passwordEncoder.encodePassword(password, ""));

        SimpleMailMessage mail = new SimpleMailMessage(newPasswordTemplate);
        StringTemplate bodyTemplate = new StringTemplate(mail.getText());
        bodyTemplate.setAttribute("password", password);
        mail.setText(bodyTemplate.toString());
        mail.setTo(user.getEmail());
        mailSender.send(mail);
    }

    /**
     * Generates a new password. This input isn't exactly super secure, but it
     * should do the trick.
     * 
     * @return A new randomly generated 12 character <code>String</code>
     *         representing an <code>User</code>'s (provisional) password.
     */
    private String generateNewPassword() {
        String randomString = passwordEncoder.encodePassword(new Date()
                .toString(), new Random().nextDouble());
        return randomString.substring(0, 12);
    }

    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public SimpleMailMessage getNewPasswordTemplate() {
        return newPasswordTemplate;
    }

    public void setNewPasswordTemplate(SimpleMailMessage newPasswordTemplate) {
        this.newPasswordTemplate = newPasswordTemplate;
    }

    public UserDAO getUserDDAO() {
        return userDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
