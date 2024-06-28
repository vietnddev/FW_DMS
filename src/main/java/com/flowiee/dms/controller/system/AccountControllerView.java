package com.flowiee.dms.controller.system;

import com.flowiee.dms.base.BaseController;
import com.flowiee.dms.entity.system.Account;
import com.flowiee.dms.exception.DataExistsException;
import com.flowiee.dms.exception.ResourceNotFoundException;
import com.flowiee.dms.model.role.ActionModel;
import com.flowiee.dms.model.role.RoleModel;
import com.flowiee.dms.service.system.AccountService;
import com.flowiee.dms.service.system.GroupAccountService;
import com.flowiee.dms.service.system.RoleService;
import com.flowiee.dms.utils.CommonUtils;
import com.flowiee.dms.utils.PagesUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sys/tai-khoan")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AccountControllerView extends BaseController {
    RoleService         roleService;
    AccountService      accountService;
    GroupAccountService groupAccountService;

    @GetMapping
    @PreAuthorize("@vldModuleSystem.readAccount(true)")
    public ModelAndView findAllAccount() {
        ModelAndView modelAndView = new ModelAndView(PagesUtils.SYS_ACCOUNT);
        modelAndView.addObject("account", new Account());
        modelAndView.addObject("listAccount", accountService.findAll());
        modelAndView.addObject("groupAccount", groupAccountService.findAll());
        return baseView(modelAndView);
    }

    @GetMapping(value = "/{id}")
    @PreAuthorize("@vldModuleSystem.readAccount(true)")
    public ModelAndView findDetailAccountById(@PathVariable("id") Integer accountId) {
        if (accountId <= 0 || accountService.findById(accountId).isEmpty()) {
            throw new ResourceNotFoundException("Account not found!", true);
        }
        ModelAndView modelAndView = new ModelAndView(PagesUtils.SYS_ACCOUNT_DETAIL);
        List<RoleModel> roleOfAccount = roleService.findAllRoleByAccountId(accountId);
        modelAndView.addObject("listRole", roleOfAccount);
        modelAndView.addObject("accountInfo", accountService.findById(accountId).get());
        modelAndView.addObject("groupAccount", groupAccountService.findAll());
        return baseView(modelAndView);
    }

    @PostMapping(value = "/insert")
    @PreAuthorize("@vldModuleSystem.insertAccount(true)")
    public ModelAndView save(@ModelAttribute("account") Account account) {
        if (accountService.findByUsername(account.getUsername()) != null) {
            throw new DataExistsException("Username exists!");
        }
        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();
        String password = account.getPassword();
        account.setPassword(bCrypt.encode(password));
        accountService.save(account);
        return baseView(new ModelAndView("redirect:/sys/tai-khoan"));
    }

    @PostMapping(value = "/update/{id}")
    @PreAuthorize("@vldModuleSystem.updateAccount(true)")
    public ModelAndView update(@ModelAttribute("account") Account accountEntity,
                               @PathVariable("id") Integer accountId,
                               HttpServletRequest request) {
        Optional<Account> accOptional = accountService.findById(accountId);
        if (accOptional.isEmpty()) {
            throw new ResourceNotFoundException("Account not found!", true);
        }
        Account account = accOptional.get();
        accountEntity.setId(accountId);
        accountEntity.setUsername(account.getUsername());
        accountEntity.setPassword(account.getPassword());
        accountEntity.setLastUpdatedBy(CommonUtils.getUserPrincipal().getUsername());
        accountService.update(accountEntity, accountId);
        return baseView(new ModelAndView("redirect:" + request.getHeader("referer")));
    }

    @PostMapping(value = "/delete/{id}")
    @PreAuthorize("@vldModuleSystem.deleteAccount(true)")
    public ModelAndView deleteAccount(@PathVariable("id") Integer accountId) {
        Optional<Account> accountOptional = accountService.findById(accountId);
        if (accountId <= 0 || accountOptional.isEmpty()) {
            throw new ResourceNotFoundException("Account not found!", true);
        }
        Account account = accountOptional.get();
        account.setStatus(false);
        accountService.save(account);
        return baseView(new ModelAndView("redirect:/sys/tai-khoan"));
    }

    @PostMapping("/update-permission/{id}")
    @PreAuthorize("@vldModuleSystem.updateAccount(true)")
    public ModelAndView updatePermission(@PathVariable("id") Integer accountId, HttpServletRequest request) {
        if (accountId <= 0 || accountService.findById(accountId).isEmpty()) {
            throw new ResourceNotFoundException("Account not found!", true);
        }
        roleService.deleteAllRole(null, accountId);
        List<ActionModel> listAction = roleService.findAllAction();
        for (ActionModel sysAction : listAction) {
            String clientActionKey = request.getParameter(sysAction.getActionKey());
            if (clientActionKey != null) {
                boolean isAuthorSelected = clientActionKey.equals("on");
                if (isAuthorSelected) {
                    roleService.updatePermission(sysAction.getModuleKey(), sysAction.getActionKey(), accountId);
                }
            }
        }
        return baseView(new ModelAndView("redirect:/sys/tai-khoan/" + accountId));
    }
}