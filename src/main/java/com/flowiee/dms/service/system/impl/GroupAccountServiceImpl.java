package com.flowiee.dms.service.system.impl;

import com.flowiee.dms.entity.system.GroupAccount;
import com.flowiee.dms.exception.BadRequestException;
import com.flowiee.dms.exception.ResourceNotFoundException;
import com.flowiee.dms.model.ACTION;
import com.flowiee.dms.model.MODULE;
import com.flowiee.dms.repository.system.GroupAccountRepository;
import com.flowiee.dms.service.BaseService;
import com.flowiee.dms.service.system.GroupAccountService;
import com.flowiee.dms.utils.ChangeLog;
import com.flowiee.dms.utils.constants.MasterObject;
import com.flowiee.dms.utils.constants.MessageCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class GroupAccountServiceImpl extends BaseService implements GroupAccountService {
    GroupAccountRepository groupAccountRepository;

    @Override
    public List<GroupAccount> findAll() {
        return this.findAll(-1, -1).getContent();
    }

    @Override
    public Page<GroupAccount> findAll(int pageSize, int pageNum) {
        Pageable pageable = Pageable.unpaged();
        if (pageSize >= 0 && pageNum >= 0) {
            pageable = PageRequest.of(pageNum, pageSize, Sort.by("groupName").ascending());
        }
        return groupAccountRepository.findAll(pageable);
    }

    @Override
    public Optional<GroupAccount> findById(Integer groupId) {
        if (groupId == null || groupId <= 0) {
            return Optional.empty();
        }
        return groupAccountRepository.findById(groupId);
    }

    @Override
    public GroupAccount save(GroupAccount groupAccount) {
        return groupAccountRepository.save(groupAccount);
    }

    @Override
    public GroupAccount update(GroupAccount groupAccount, Integer groupId) {
        Optional<GroupAccount> groupAccountOpt = this.findById(groupId);
        if (groupAccountOpt.isEmpty()) {
            throw new ResourceNotFoundException("Group account not found", false);
        }
        GroupAccount groupAccountBefore = ObjectUtils.clone(groupAccountOpt.get());

        groupAccount.setId(groupId);
        GroupAccount groupAccountUpdated = groupAccountRepository.save(groupAccount);

        ChangeLog changeLog = new ChangeLog(groupAccountBefore, groupAccountUpdated);
        systemLogService.writeLogUpdate(MODULE.SYSTEM, ACTION.SYS_GR_ACC_U, MasterObject.GroupAccount, "Cập nhật thông tin nhóm người dùng", changeLog.getOldValues(), changeLog.getNewValues());

        return groupAccountUpdated;
    }

    @Override
    public String delete(Integer groupId) {
        if (this.findById(groupId).isEmpty()) {
            throw new BadRequestException();
        }
        groupAccountRepository.deleteById(groupId);
        return MessageCode.DELETE_SUCCESS.getDescription();
    }
}