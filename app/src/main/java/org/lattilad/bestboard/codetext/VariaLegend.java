package org.lattilad.bestboard.codetext;

/**
 * Created by Beothe on 2016.08.20..
 */
public class VariaLegend
    {
    private String text;
    private String title;

    public VariaLegend(String text)
        {
        this( text, text );
        }

    public VariaLegend(String text, String title)
        {
        this.text = text;
        this.title = title;
        }

    public String getText()
        {
        return text;
        }

    public String getTitle()
        {
        return title;
        }
    }
