/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.socialsensor.trendslabeler;

public class RankedTitle implements Comparable<RankedTitle> {

    private String title;        // tag label
    private int frequency;         // tag weight


    public RankedTitle(String title, int frequency) {
        this.title = title;
        this.frequency = frequency;
    }

    public String getTitle() {
        return title;
    }

    public int getFrequency() {
        return frequency;
    }
    public void setFrequency(int  newFrequency){
        this.frequency = newFrequency;
    }

    public int increaseFrequency(){
        frequency++;
        return frequency;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RankedTitle){
            return title.equals( ((RankedTitle)obj).getTitle() );
        }
        return false;
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    public int compareTo(RankedTitle o) {
        if (this.getFrequency() < o.getFrequency()){
            return -1;
        } else if (this.getFrequency() > o.getFrequency()) {
            return 1;
        } else {
            return 0;
        }
    }
}
