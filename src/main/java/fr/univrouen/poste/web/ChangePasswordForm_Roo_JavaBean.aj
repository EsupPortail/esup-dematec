// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.web;

import fr.univrouen.poste.web.ChangePasswordForm;

privileged aspect ChangePasswordForm_Roo_JavaBean {
    
    public String ChangePasswordForm.getOldPassword() {
        return this.oldPassword;
    }
    
    public void ChangePasswordForm.setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
    
    public String ChangePasswordForm.getNewPassword() {
        return this.newPassword;
    }
    
    public void ChangePasswordForm.setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    public String ChangePasswordForm.getNewPasswordAgain() {
        return this.newPasswordAgain;
    }
    
    public void ChangePasswordForm.setNewPasswordAgain(String newPasswordAgain) {
        this.newPasswordAgain = newPasswordAgain;
    }
    
}