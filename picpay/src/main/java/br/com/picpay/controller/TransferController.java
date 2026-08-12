package br.com.picpay.controller;

import br.com.picpay.dto.request.TransferRequest;
import br.com.picpay.dto.response.TransferResponse;
import br.com.picpay.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) { this.transferService = transferService; }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        return transferService.transfer(request);
    }
}
