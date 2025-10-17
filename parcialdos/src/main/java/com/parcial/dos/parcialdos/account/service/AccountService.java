package com.parcial.dos.parcialdos.account.service;

import com.parcial.dos.parcialdos.account.dto.AccountRequestDTO;
import com.parcial.dos.parcialdos.account.dto.AccountResponseDTO;
import com.parcial.dos.parcialdos.account.dto.AccountOwnerBalanceDTO;
import com.parcial.dos.parcialdos.account.entity.Account;
import com.parcial.dos.parcialdos.account.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService implements IAccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public AccountResponseDTO create(AccountRequestDTO request) {
        accountRepository.findByAccountNumber(request.getNumeroCuenta())
            .ifPresent(account -> {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Ya existe una cuenta con el número: " + request.getNumeroCuenta()
                );
            });

        Account account = new Account(
            request.getNumeroCuenta(),
            request.getDueno(),
            request.getBalanceActual(),
            request.getActiva()
        );

        Account savedAccount = accountRepository.save(account);
        return mapToResponseDTO(savedAccount);
    }

    @Override
    public List<AccountResponseDTO> getAll() {
        return accountRepository.findAll().stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    @Override
    public AccountResponseDTO getById(Long id) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Cuenta no encontrada con id: " + id
            ));

        return mapToResponseDTO(account);
    }

    @Override
    public String update(Long id, AccountRequestDTO request) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Cuenta no encontrada con id: " + id
            ));

        // Actualización parcial: solo actualizar campos no nulos
        if (request.getNumeroCuenta() != null && !request.getNumeroCuenta().isEmpty()) {
            if (!account.getAccountNumber().equals(request.getNumeroCuenta())) {
                accountRepository.findByAccountNumber(request.getNumeroCuenta())
                    .ifPresent(existingAccount -> {
                        throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, 
                            "Ya existe una cuenta con el número: " + request.getNumeroCuenta()
                        );
                    });
            }
            account.setAccountNumber(request.getNumeroCuenta());
        }

        if (request.getDueno() != null && !request.getDueno().isEmpty()) {
            account.setOwnerName(request.getDueno());
        }

        if (request.getBalanceActual() != null) {
            account.setBalance(request.getBalanceActual());
        }

        if (request.getActiva() != null) {
            account.setActive(request.getActiva());
        }

        accountRepository.save(account);
        return "Cuenta actualizada exitosamente";
    }

    @Override
    public void delete(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Cuenta no encontrada con id: " + id
            );
        }

        accountRepository.deleteById(id);
    }

    @Override
    public AccountOwnerBalanceDTO findByNumeroCuenta(String numeroCuenta) {
        Account account = accountRepository.findByAccountNumber(numeroCuenta)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Cuenta no encontrada con número: " + numeroCuenta
            ));
        
        return new AccountOwnerBalanceDTO(
            account.getOwnerName(),
            account.getBalance()
        );
    }

    private AccountResponseDTO mapToResponseDTO(Account account) {
        return new AccountResponseDTO(
            account.getId(),
            account.getAccountNumber(),
            account.getOwnerName(),
            account.getBalance(),
            account.getActive()
        );
    }
}