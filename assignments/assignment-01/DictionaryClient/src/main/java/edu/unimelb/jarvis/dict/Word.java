package edu.unimelb.jarvis.dict;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Word implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String word;
    private String meaning;
}
