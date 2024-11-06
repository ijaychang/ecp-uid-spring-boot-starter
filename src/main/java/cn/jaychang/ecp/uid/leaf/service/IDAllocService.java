package cn.jaychang.ecp.uid.leaf.service;


import cn.jaychang.ecp.uid.leaf.model.LeafAlloc;

import java.util.List;

public interface IDAllocService {
     List<LeafAlloc> getAllLeafAllocs();
     LeafAlloc updateMaxIdAndGetLeafAlloc(String tag);
     LeafAlloc updateMaxIdByCustomStepAndGetLeafAlloc(LeafAlloc leafAlloc);
     List<String> getAllTags();
}
