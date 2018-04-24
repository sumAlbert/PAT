package entity;

import java.util.*;

public class Production {
    private Set<String> unstopSymbols = new HashSet<>();
    private Set<String> stopSymbols = new HashSet<>();
    private Map<String,List> generators = new HashMap<>();
    private String derivationSymbol = "->";
    private String separateSymbol = "|";
    private String startSymbol = "";
    private String currentUnstop = "";

    public Map<String, List> getGenerators() {
        return generators;
    }
    public Set<String> getStopSymbols() {
        return stopSymbols;
    }
    public Set<String> getUnstopSymbols() {
        return unstopSymbols;
    }
    public String getDerivationSymbol() {
        return derivationSymbol;
    }
    public String getSeparateSymbol() {
        return separateSymbol;
    }
    public String getStartSymbol() {
        return startSymbol;
    }
    public String getCurrentUnstop() {
        return currentUnstop;
    }

    public void setDerivationSymbol(String derivationSymbol) {
        this.derivationSymbol = derivationSymbol;
    }
    public void setGenerators(Map<String, List> generators) {
        this.generators = generators;
    }
    public void setSeparateSymbol(String separateSymbol) {
        this.separateSymbol = separateSymbol;
    }
    public void setStartSymbol(String startSymbol) {
        this.startSymbol = startSymbol;
    }
    public void setStopSymbols(Set<String> stopSymbols) {
        this.stopSymbols = stopSymbols;
    }
    public void setUnstopSymbols(Set<String> unstopSymbols) {
        this.unstopSymbols = unstopSymbols;
    }
    public void setCurrentUnstop(String currentUnstop) {
        this.currentUnstop = currentUnstop;
    }

    public String getSplitDerivation (){
        String result = this.derivationSymbol;
        if(result.equals(".")){
            result = "\\.";
        } else if(result.equals("|")){
            result =  "\\|";
        } else if(result.equals("\\")){
            result = "\\\\";
        }
        return result;
    }
    public String getSplitSeparator () {
        String result = this.separateSymbol;
        if(result.equals(".")){
            result = "\\.";
        } else if(result.equals("|")){
            result =  "\\|";
        } else if(result.equals("\\")){
            result = "\\\\";
        }
        return result;
    }
}
