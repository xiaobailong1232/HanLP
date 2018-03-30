/*
 * <author>Hankcs</author>
 * <email>me@hankcs.com</email>
 * <create-date>2018-03-30 上午1:07</create-date>
 *
 * <copyright file="CRFSegmenter.java" company="码农场">
 * Copyright (c) 2018, 码农场. All Right Reserved, http://www.hankcs.com/
 * This source is subject to Hankcs. Please contact Hankcs to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.model.crf;

import com.hankcs.hanlp.corpus.document.sentence.Sentence;
import com.hankcs.hanlp.corpus.document.sentence.word.Word;
import com.hankcs.hanlp.dictionary.other.CharTable;
import com.hankcs.hanlp.model.crf.crfpp.TaggerImpl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
//import static com.hankcs.hanlp.classification.utilities.io.ConsoleLogger.logger;

/**
 * @author hankcs
 */
public class CRFSegmenter extends CRFTagger
{
    public CRFSegmenter()
    {
    }

    public CRFSegmenter(String modelPath) throws IOException
    {
        super(modelPath);
    }

    @Override
    protected void convertCorpus(Sentence sentence, BufferedWriter bw) throws IOException
    {
        for (Word w : sentence.toSimpleWordList())
        {
            String word = CharTable.convert(w.value);
            if (word.length() == 1)
            {
                bw.write(word);
                bw.write('\t');
                bw.write('S');
                bw.write('\n');
            }
            else
            {
                bw.write(word.charAt(0));
                bw.write('\t');
                bw.write('B');
                bw.write('\n');
                for (int i = 1; i < word.length() - 1; ++i)
                {
                    bw.write(word.charAt(i));
                    bw.write('\t');
                    bw.write('M');
                    bw.write('\n');
                }
                bw.write(word.charAt(word.length() - 1));
                bw.write('\t');
                bw.write('E');
                bw.write('\n');
            }
        }
        bw.newLine();
    }

    public List<String> segment(String text)
    {
        List<String> wordList = new LinkedList<String>();
        if (text.isEmpty()) return wordList;
        TaggerImpl tagger = new TaggerImpl(TaggerImpl.Mode.TEST);
        tagger.setModel(this.model);
        for (int i = 0; i < text.length(); i++)
        {
            tagger.add(String.valueOf(CharTable.convert(text.charAt(i))));
        }
        if (!tagger.parse()) return wordList;

        StringBuilder result = new StringBuilder();
        result.append(text.charAt(0));
//        if (text.length() != tagger.size())
//        {
//            System.err.println(text);
//        }

        for (int i = 1; i < tagger.size(); i++)
        {
            char tag = tagger.yname(tagger.y(i)).charAt(0);
            if (tag == 'B' || tag == 'S')
            {
                wordList.add(result.toString());
                result.setLength(0);
            }
            result.append(text.charAt(i));
        }
        if (result.length() != 0)
        {
            wordList.add(result.toString());
        }

        return wordList;
    }

    @Override
    protected String getFeatureTemplate()
    {
        return "# Unigram\n" +
            "U0:%x[-1,0]\n" +
            "U1:%x[0,0]\n" +
            "U2:%x[1,0]\n" +
            "U3:%x[-1,0]%x[0,0]\n" +
            "U4:%x[0,0]%x[1,0]\n" +
            "U5:%x[-1,0]%x[1,0]\n" +
            "\n" +
            "# Bigram\n" +
            "B";
    }
}
