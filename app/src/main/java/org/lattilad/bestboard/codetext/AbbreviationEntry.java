package org.lattilad.bestboard.codetext;

public class AbbreviationEntry extends Entry
    {
    private String expanded;

    public AbbreviationEntry(String code, String expanded )
        {
        super(code);
        this.expanded = expanded;
        }

    public String getExpanded()
        {
        return expanded;
        }

    }
