// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.domain;

import fr.univrouen.poste.domain.BigFile;
import fr.univrouen.poste.domain.MemberReviewFile;
import fr.univrouen.poste.domain.User;
import java.util.Date;
import org.springframework.web.multipart.MultipartFile;

privileged aspect MemberReviewFile_Roo_JavaBean {
    
    public String MemberReviewFile.getFilename() {
        return this.filename;
    }
    
    public void MemberReviewFile.setFilename(String filename) {
        this.filename = filename;
    }
    
    public MultipartFile MemberReviewFile.getFile() {
        return this.file;
    }
    
    public void MemberReviewFile.setFile(MultipartFile file) {
        this.file = file;
    }
    
    public Date MemberReviewFile.getSendTime() {
        return this.sendTime;
    }
    
    public void MemberReviewFile.setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }
    
    public Long MemberReviewFile.getFileSize() {
        return this.fileSize;
    }
    
    public void MemberReviewFile.setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String MemberReviewFile.getContentType() {
        return this.contentType;
    }
    
    public void MemberReviewFile.setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public User MemberReviewFile.getMember() {
        return this.member;
    }
    
    public void MemberReviewFile.setMember(User member) {
        this.member = member;
    }
    
    public BigFile MemberReviewFile.getBigFile() {
        return this.bigFile;
    }
    
    public void MemberReviewFile.setBigFile(BigFile bigFile) {
        this.bigFile = bigFile;
    }
    
}