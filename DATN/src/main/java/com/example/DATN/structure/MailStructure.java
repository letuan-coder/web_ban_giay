package com.example.DATN.structure;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MailStructure {
    private String to;
    private String subject;
    private String content;
}
