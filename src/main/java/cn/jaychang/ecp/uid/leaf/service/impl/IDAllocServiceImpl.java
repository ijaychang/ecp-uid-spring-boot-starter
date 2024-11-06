package cn.jaychang.ecp.uid.leaf.service.impl;

import cn.jaychang.ecp.uid.leaf.dao.IDAllocDao;
import cn.jaychang.ecp.uid.leaf.model.LeafAlloc;
import cn.jaychang.ecp.uid.leaf.service.IDAllocService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class IDAllocServiceImpl implements IDAllocService {

    private final IDAllocDao idAllocDao;

    public IDAllocServiceImpl(IDAllocDao idAllocDao) {
        this.idAllocDao = idAllocDao;
    }


    @Override
    public List<LeafAlloc> getAllLeafAllocs() {
        return idAllocDao.getAllLeafAllocs();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeafAlloc updateMaxIdAndGetLeafAlloc(String tag) {
        idAllocDao.updateMaxId(tag);
        return idAllocDao.getLeafAlloc(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeafAlloc updateMaxIdByCustomStepAndGetLeafAlloc(LeafAlloc leafAlloc) {
        idAllocDao.updateMaxIdByCustomStep(leafAlloc);
        return idAllocDao.getLeafAlloc(leafAlloc.getKey());
    }

    @Override
    public List<String> getAllTags() {
        return idAllocDao.getAllTags();
    }
}
