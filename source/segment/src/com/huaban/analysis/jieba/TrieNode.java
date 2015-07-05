package com.huaban.analysis.jieba;

import java.io.Serializable;
import java.util.HashMap;

public class TrieNode implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public char key = (char)0;

    public HashMap<Character, TrieNode> childs = new HashMap<Character, TrieNode>();
    
    public TrieNode() {}
    
    public TrieNode(char key) {
        this.key = key;
    }
}
