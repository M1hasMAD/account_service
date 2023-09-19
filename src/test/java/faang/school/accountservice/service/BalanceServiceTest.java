package faang.school.accountservice.service;

import faang.school.accountservice.dto.BalanceDto;
import faang.school.accountservice.entity.account.Account;
import faang.school.accountservice.entity.balance.Balance;
import faang.school.accountservice.excpetion.EntityNotFoundException;
import faang.school.accountservice.excpetion.InsufficientBalanceException;
import faang.school.accountservice.mapper.BalanceMapperImpl;
import faang.school.accountservice.repository.BalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BalanceServiceTest {

    @InjectMocks
    private BalanceService balanceService;
    @Mock
    private BalanceRepository balanceRepository;
    @Mock
    private BalanceMapperImpl balanceMapper;
    @Mock
    private AccountService accountService;

    private BalanceDto balanceDto;
    private Balance balance;
    private Account account;


    @BeforeEach
    public void setUp() {
        account = Account.builder().build();
        balanceDto = BalanceDto.builder().build();
        balance = Balance.builder().account(account)
                .authorizationBalance(BigDecimal.ZERO)
                .actualBalance(BigDecimal.ZERO)
                .build();
    }

    @Test
    public void testGetBalanceByAccountIdSuccess() {
        when(balanceRepository.findByAccountId(1L)).thenReturn(Optional.of(balance));
        when(balanceMapper.toDto(balance)).thenReturn(balanceDto);

        BalanceDto result = balanceService.getBalanceByAccountId(1L);
        assertNotNull(result);
        assertEquals(balanceDto, result);
    }

    @Test
    public void testCreateBalanceSuccess() {
        when(accountService.getAccount(1L)).thenReturn(account);
        when(balanceRepository.save(any(Balance.class))).thenReturn(balance);
        when(balanceMapper.toDto(balance)).thenReturn(balanceDto);

        BalanceDto result = balanceService.createBalance(1L, balanceDto);
        assertNotNull(result);
        assertEquals(balanceDto, result);
    }

    @Test
    public void testUpdateBalanceSuccess() {
        when(balanceRepository.findByAccountId(1L)).thenReturn(java.util.Optional.of(balance));
        when(balanceRepository.save(any(Balance.class))).thenReturn(balance);
        when(balanceMapper.toDto(balance)).thenReturn(balanceDto);

        BalanceDto result = balanceService.updateBalance(1L, balanceDto);
        assertNotNull(result);
        assertEquals(balanceDto, result);
    }

    @Test
    public void testGetBalanceByAccountIdFail() {
        when(balanceRepository.findByAccountId(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> balanceService.getBalanceByAccountId(1L));
    }

    @Test
    public void testDepositSuccess() {
        balance.setAuthorizationBalance(BigDecimal.ZERO);
        balance.setActualBalance(BigDecimal.ZERO);

        Balance savedBalance = new Balance();
        savedBalance.setAuthorizationBalance(BigDecimal.TEN);
        savedBalance.setActualBalance(BigDecimal.TEN);

        BalanceDto expectedDto = BalanceDto.builder()
                .authorizationBalance(savedBalance.getAuthorizationBalance())
                .actualBalance(savedBalance.getActualBalance())
                .build();

        when(balanceRepository.findByAccountId(1L)).thenReturn(Optional.of(balance));
        when(balanceRepository.save(balance)).thenReturn(savedBalance);
        when(balanceMapper.toDto(savedBalance)).thenReturn(expectedDto);

        BalanceDto result = balanceService.deposit(1L, BigDecimal.TEN);

        assertEquals(BigDecimal.TEN, result.getAuthorizationBalance());
        assertEquals(BigDecimal.TEN, result.getActualBalance());

        verify(balanceRepository, times(1)).findByAccountId(1L);
        verify(balanceRepository, times(1)).save(balance);
        verify(balanceMapper, times(1)).toDto(savedBalance);
    }

    @Test
    public void testDepositInvalidAccountId() {
        Long invalidAccountId = -1L;
        BigDecimal amount = BigDecimal.TEN;

        assertThrows(EntityNotFoundException.class, () -> balanceService.deposit(invalidAccountId, amount));

        verifyNoInteractions(balanceMapper);
    }

    @Test
    public void testDepositNegativeAmount() {
        Long accountId = 1L;
        BigDecimal negativeAmount = BigDecimal.valueOf(-10);

        assertThrows(EntityNotFoundException.class, () -> balanceService.deposit(accountId, negativeAmount));

        verifyNoInteractions(balanceMapper);
    }

    @Test
    public void testDepositBalanceNotFound() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.TEN;

        when(balanceRepository.findByAccountId(accountId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> balanceService.deposit(accountId, amount));

        verify(balanceRepository, times(1)).findByAccountId(accountId);
        verifyNoInteractions(balanceMapper);
    }

    @Test
    void testWithdrawWithSufficientAuthorizationBalanceSuccess() {
        Long accountId = 123L;
        BigDecimal amount = new BigDecimal("100.00");

        balance.setAuthorizationBalance(new BigDecimal("200.00"));
        balance.setActualBalance(new BigDecimal("500.00"));

        when(balanceRepository.findByAccountId(accountId)).thenReturn(Optional.ofNullable(balance));

        Balance updatedBalance = new Balance();
        updatedBalance.setAuthorizationBalance(new BigDecimal("100.00"));
        updatedBalance.setActualBalance(new BigDecimal("400.00"));

        when(balanceRepository.save(balance)).thenReturn(updatedBalance);

        BalanceDto expectedDto = new BalanceDto();
        expectedDto.setAuthorizationBalance(new BigDecimal("100.00"));
        expectedDto.setActualBalance(new BigDecimal("400.00"));

        when(balanceMapper.toDto(updatedBalance)).thenReturn(expectedDto);

        BalanceDto result = balanceService.withdraw(accountId, amount);

        assertEquals(expectedDto, result);
        assertEquals(new BigDecimal("100.00"), balance.getAuthorizationBalance());
        assertEquals(new BigDecimal("400.00"), balance.getActualBalance());
    }

    @Test
    void testWithdrawWithInsufficientAuthorizationBalanceFail() {
        Long accountId = 123L;
        BigDecimal amount = new BigDecimal("100.00");

        balance.setAuthorizationBalance(new BigDecimal("50.00"));
        balance.setActualBalance(new BigDecimal("500.00"));

        when(balanceRepository.findByAccountId(accountId)).thenReturn(Optional.ofNullable(balance));

        assertThrows(InsufficientBalanceException.class, () -> balanceService.withdraw(accountId, amount));
    }

    @Test
    void testTransferWithSufficientAuthorizationBalanceSuccess() {
        Long senderId = 123L;
        Long receiverId = 456L;
        BigDecimal amount = new BigDecimal("100.00");

        Balance senderBalance = new Balance();
        senderBalance.setAuthorizationBalance(new BigDecimal("200.00"));
        senderBalance.setActualBalance(new BigDecimal("500.00"));

        when(balanceRepository.findByAccountId(senderId)).thenReturn(Optional.of(senderBalance));

        Balance receiverBalance = new Balance();
        receiverBalance.setActualBalance(new BigDecimal("300.00"));

        when(balanceRepository.findByAccountId(receiverId)).thenReturn(Optional.of(receiverBalance));

        Balance updatedSenderBalance = new Balance();
        updatedSenderBalance.setAuthorizationBalance(new BigDecimal("100.00"));
        updatedSenderBalance.setActualBalance(new BigDecimal("400.00"));

        when(balanceRepository.save(senderBalance)).thenReturn(updatedSenderBalance);

        Balance updatedReceiverBalance = new Balance();
        updatedReceiverBalance.setActualBalance(new BigDecimal("400.00"));

        when(balanceRepository.save(receiverBalance)).thenReturn(updatedReceiverBalance);

        balanceService.transfer(senderId, receiverId, amount);

        assertEquals(new BigDecimal("100.00"), senderBalance.getAuthorizationBalance());
        assertEquals(new BigDecimal("400.00"), senderBalance.getActualBalance());
        assertEquals(new BigDecimal("400.00"), receiverBalance.getActualBalance());
    }

    @Test
    void testTransferWithInsufficientAuthorizationBalanceFail() {
        Long senderId = 123L;
        Long receiverId = 456L;
        BigDecimal amount = new BigDecimal("100.00");

        Balance senderBalance = new Balance();
        senderBalance.setAuthorizationBalance(new BigDecimal("50.00"));
        senderBalance.setActualBalance(new BigDecimal("500.00"));

        when(balanceRepository.findByAccountId(senderId)).thenReturn(Optional.of(senderBalance));

        Balance receiverBalance = new Balance();
        receiverBalance.setActualBalance(new BigDecimal("300.00"));

        when(balanceRepository.findByAccountId(receiverId)).thenReturn(Optional.of(receiverBalance));

        assertThrows(InsufficientBalanceException.class, () -> balanceService.transfer(senderId, receiverId, amount));
    }
}
