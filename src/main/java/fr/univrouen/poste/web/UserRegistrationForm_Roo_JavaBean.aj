// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.web;

import fr.univrouen.poste.web.UserRegistrationForm;

privileged aspect UserRegistrationForm_Roo_JavaBean {
    
    public String UserRegistrationForm.getFirstName() {
        return this.firstName;
    }

    public String UserRegistrationForm.getLastName() {
        return this.lastName;
    }

    public String UserRegistrationForm.getEmailAddress() {
        return this.emailAddress;
    }
    
    public String UserRegistrationForm.getPassword() {
        return this.password;
    }
    
    public void UserRegistrationForm.setPassword(String password) {
        this.password = password;
    }
    
    public String UserRegistrationForm.getRepeatPassword() {
        return this.repeatPassword;
    }
    
    public void UserRegistrationForm.setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }
    
    public String UserRegistrationForm.getActivationKey() {
        return this.activationKey;
    }
    
    public void UserRegistrationForm.setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }
    
}
