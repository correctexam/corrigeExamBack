package fr.istic.service.customdto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class MailResultDTO {
    private String subject;
    private String body;
    private boolean mailabi;
    private boolean mailpdf;
    private String bodyabi;
    private String sheetuuid;



    public String getBodyabi() {
        return this.bodyabi;
    }

    public void setBodyabi(String bodyabi) {
        this.bodyabi = bodyabi;
    }

    public boolean isMailabi() {
        return this.mailabi;
    }

    public boolean getMailabi() {
        return this.mailabi;
    }

    public void setMailabi(boolean mailabi) {
        this.mailabi = mailabi;
    }

    public boolean isMailpdf() {
        return this.mailpdf;
    }

    public boolean getMailpdf() {
        return this.mailpdf;
    }

    public void setMailpdf(boolean mailpdf) {
        this.mailpdf = mailpdf;
    }


    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }

    public String getSheetuuid() {
        return this.sheetuuid;
    }

    public void setSheetuuid(String sheetuuid) {
        this.sheetuuid = sheetuuid;
    }
    @Override
    public String toString() {
        return "MailResultDTO [body=" + body + ", subject=" + subject + "]";
    }




}
