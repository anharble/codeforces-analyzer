package com.codeforces.analyzer.controller;

import java.util.List;

import com.codeforces.analyzer.crawler.CodeforcesLoginService;
import com.codeforces.analyzer.model.CfAccount;
import com.codeforces.analyzer.service.AccountService;

public class AccountController {
    private final AccountService accountService;
    private final CodeforcesLoginService loginService;

    public AccountController(AccountService accountService, CodeforcesLoginService loginService) {
        this.accountService = accountService;
        this.loginService = loginService;
    }

    public CfAccount addAccount(String username, String password) {
        return accountService.addAccount(username, password);
    }

    public void updateAccount(long id, String username, String passwordOrMask) {
        accountService.updateAccount(id, username, passwordOrMask);
    }

    public void deleteAccount(long id) {
        accountService.deleteAccount(id);
    }

    public List<CfAccount> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    public CfAccount findFirstAvailable() {
        return accountService.findFirstAvailable();
    }

    public boolean login(CfAccount account) {
        return loginService.dangNhap(account);
    }

    public boolean checkLogin(CfAccount account) {
        return loginService.kiemTraDangNhap(account);
    }

    public void logout(CfAccount account) {
        loginService.dangXuat(account);
    }
}
