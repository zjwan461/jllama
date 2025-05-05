package com.itsu.jllama.domain.model;

import lombok.Data;

@Data
public class ModelScopeModelFile implements ModelFile {

    private String CommitMessage;
    private long CommittedDate;
    private String CommitterName;
    private boolean InCheck;
    private boolean IsLFS;
    private String Mode;
    private String Name;
    private String Path;
    private String Revision;
    private String Sha256;
    private long Size;
    private String Type;
}
