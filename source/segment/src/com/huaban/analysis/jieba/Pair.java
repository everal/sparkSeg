package com.huaban.analysis.jieba;

import java.io.Serializable;

public class Pair<K> implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public K key;
    public Double freq = 0.0;

    public Pair(K key, double freq) {
	this.key = key;
	this.freq = freq;
    }

    @Override
    public String toString() {
	return "Candidate [key=" + key + ", freq=" + freq + "]";
    }

}
