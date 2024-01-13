package com.kopchak.worldoftoys.domain.email;

import lombok.Getter;

@Getter
public abstract class EmailType {
    private final String title;
    private final String subject;
    private final String link;
    private final String linkName;
    private final String msg;

    public EmailType(String title, String subject, String link, String linkName, String msg) {
        this.title = title;
        this.subject = subject;
        this.link = link;
        this.linkName = linkName;
        this.msg = msg;
    }
}
