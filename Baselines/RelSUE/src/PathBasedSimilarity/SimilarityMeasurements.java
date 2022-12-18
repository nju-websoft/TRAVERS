package PathBasedSimilarity;

import Path.MetaPath;
import Path.RelationPath;

public interface SimilarityMeasurements {
	public String ID = null;
	public double getSim(int a, int b, RelationPath rp);
	public double getSim(int a, int b, MetaPath mp);
}
