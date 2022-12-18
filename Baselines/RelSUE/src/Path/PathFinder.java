package Path;

import java.util.*;

public interface PathFinder {
	public Set<RelationPath> findRelationPath(int query, List<Integer> examples);
	@Deprecated
	public Set<MetaPath> findMetaPath(int query, List<Integer> examples);
}
