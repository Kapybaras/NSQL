package uk.ac.bangor.cs.dyp24nbv.dbManager;

import java.util.ArrayList;

public interface Transferable<t> {
	ArrayList<ArrayList<t>> getAllparametersFromStatement(String SqlStatement);
}
