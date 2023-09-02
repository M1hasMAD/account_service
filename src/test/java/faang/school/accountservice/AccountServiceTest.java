package faang.school.accountservice;

import faang.school.accountservice.dto.AccountDto;
import faang.school.accountservice.enums.AccountStatus;
import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.enums.Currency;
import faang.school.accountservice.mapper.AccountMapper;
import faang.school.accountservice.model.Account;
import faang.school.accountservice.repository.AccountRepository;
import faang.school.accountservice.service.AccountService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;

public class AccountServiceTest {

    private AccountDto accountDto;
    private AccountMapper accountMapper;
    private AccountService accountService;
    private AccountRepository accountRepository;

    @BeforeEach
    public void setUp() {
          accountRepository = Mockito.mock(AccountRepository.class);
          accountMapper = Mockito.mock(AccountMapper.class);
          accountService = new AccountService(accountRepository, accountMapper);
          accountDto = AccountDto.builder()
                .id(1L)
                .accountNumber("1234567890")
                .typeOfOwner("owner")
                .ownerId(1L)
                .accountType(AccountType.CREDIT)
                .currency(Currency.USD)
                .status(AccountStatus.CURRENT)
                .version(2L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    public void getAccountTest() {
        Account account = new Account();
        account.setId(1L);
        Mockito.when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        Mockito.when(accountMapper.accountToAccountDto(account)).thenReturn(accountDto);
        AccountDto result = accountService.getAccount(1L);
        Assertions.assertEquals(accountDto, result);
        Assertions.assertEquals(account.getVersion() + 1, result.getVersion());
    }

    @Test
    public void openAccountTest() {
        Account account = new Account();
        account.setId(1L);
        Mockito.when(accountMapper.accountDtoToAccount(accountDto)).thenReturn(account);
        Mockito.when(accountMapper.accountToAccountDto(account)).thenReturn(accountDto);
        Mockito.when(accountRepository.save(account)).thenReturn(account);
        AccountDto result = accountService.openAccount(accountDto);
        Assertions.assertEquals(accountDto, result);
        Assertions.assertEquals(account.getVersion() + 1, result.getVersion());
    }

    @Test
    public void freezeAccountTest() {
        Account account = new Account();
        account.setId(1L);
        Mockito.when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        Mockito.when(accountRepository.save(account)).thenReturn(account);
        Mockito.when(accountMapper.accountToAccountDto(account)).thenReturn(accountDto);
        AccountDto result = accountService.freezeAccount(1L);
        Assertions.assertEquals(AccountStatus.FROZEN, account.getStatus());
        Assertions.assertEquals(account.getVersion() + 1, result.getVersion());
    }

    @Test
    public void blockAccountTest() {
        Account account = new Account();
        account.setId(1L);
        Mockito.when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        Mockito.when(accountRepository.save(account)).thenReturn(account);
        Mockito.when(accountMapper.accountToAccountDto(account)).thenReturn(accountDto);
        AccountDto result = accountService.blockAccount(1L);
        Assertions.assertEquals(AccountStatus.BLOCKED, account.getStatus());
        Assertions.assertEquals(account.getVersion() + 1, result.getVersion());
    }

    @Test
    public void closeAccountTest() {
        Account account = new Account();
        account.setId(1L);
        Mockito.when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        Mockito.when(accountRepository.save(account)).thenReturn(account);
        Mockito.when(accountMapper.accountToAccountDto(account)).thenReturn(accountDto);
        AccountDto result = accountService.closeAccount(1L);
        Assertions.assertEquals(AccountStatus.CLOSED, account.getStatus());
        Assertions.assertEquals(account.getVersion() + 1, result.getVersion());
    }
}