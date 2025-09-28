package espol.integradora.paganinibackend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import espol.integradora.paganinibackend.model.*;
import espol.integradora.paganinibackend.model.constantes.*;
import espol.integradora.paganinibackend.Dto.*;
import espol.integradora.paganinibackend.repository.*;;

@Service
public class PaymentMethodService {
    private final MetodoPagoRepository mpRepo;
    private final CardRepository cardRepo;
    private final BankAccountRepository baRepo;
    private final EwalletRepository ewRepo;
    private final CriptocoinRepository ccRepo;
    private final UserRepository userRepo;


    public PaymentMethodService(
            MetodoPagoRepository mpRepo,
            CardRepository cardRepo,
            BankAccountRepository baRepo,
            EwalletRepository ewRepo,
            CriptocoinRepository ccRepo,UserRepository userRepo) {
        this.mpRepo = mpRepo;
        this.cardRepo = cardRepo;
        this.baRepo = baRepo;
        this.ewRepo = ewRepo;
        this.ccRepo = ccRepo;
        this.userRepo    = userRepo;

    }

    @Transactional
    public MetodoPago create(PaymentMethodDto dto) {
        User user = userRepo.findByCorreo(dto.correo())
        .orElseThrow(() ->
            new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Usuario no encontrado con correo " + dto.correo()
            )
        );
        MetodoPago mp = new MetodoPago();
        mp.setUserId(user.getId());
        mp.setTipo(TipoPago.valueOf(dto.tipo()));
        mp.setEstado(EstadoPago.activo);

        switch (mp.getTipo()) {
            case tarjeta -> {
                CardDto c = dto.card();
                Card entity = new Card();
                entity.setMetodoPago(mp);
                entity.setNumeroTarjeta(c.numeroTarjeta());
                entity.setTitular(c.titular());
                entity.setMes(c.mes());
                entity.setYear(c.year());
                entity.setCvv(c.cvv());
                entity.setTipo(c.tipo().charAt(0));
                entity.setRed(RedTarjeta.valueOf(c.red()));
                mp.setCard(entity);
            }
            case cuentabanco -> {
                BankAccountDto b = dto.bankAccount();
                BankAccount entity = new BankAccount();
                entity.setMetodoPago(mp);
                entity.setNombreBanco(b.nombreBanco());
                entity.setNumeroCuenta(b.numeroCuenta());
                entity.setTipoCuenta(TipoCuenta.valueOf(b.tipoCuenta()));
                entity.setTitular(b.titular());
                entity.setIdentificacion(b.identificacion());
                mp.setBankAccount(entity);
            }
            case ewallet -> {
                EwalletDto e = dto.ewallet();
                Criptocoin crypto = ccRepo
                .findByAbreviacion(e.criptocoinAbreviacion().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Criptomoneda no válida: " + e.criptocoinAbreviacion()
                ));
                Ewallet entity = new Ewallet();
                entity.setMetodoPago(mp);
                entity.setDireccion(e.direccion());
                entity.setCriptocoin(crypto);
                mp.setEwallet(entity);
            }
        }
        return mpRepo.save(mp);
    }

    /** Marca el método como activo. */
    @Transactional
    public void activate(Integer id) {
        MetodoPago mp = mpRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Método no encontrado"));
        mp.setEstado(EstadoPago.activo);
    }

    /**
     * ACtualiza datos de un objeto
     * @param id
     */
    @Transactional
    public MetodoPago update(Integer id, PaymentMethodUpdateDto dto) {
        MetodoPago mp = mpRepo.findById(id)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Método no encontrado")
            );

        if (!mp.getTipo().name().equalsIgnoreCase(dto.tipo())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "No puede cambiar el tipo de método"
            );
        }

        switch (mp.getTipo()) {
            case cuentabanco -> {
                BankAccount ba = mp.getBankAccount();
                if (ba == null) throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No existe BankAccount para este método"
                );
                BankAccountDto d = dto.bankAccount();
                if (d == null) throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Falta datos de bankAccount"
                );
                ba.setNombreBanco(d.nombreBanco());
                ba.setNumeroCuenta(d.numeroCuenta());      
                ba.setTipoCuenta(TipoCuenta.valueOf(d.tipoCuenta()));
                ba.setTitular(d.titular());
                ba.setIdentificacion(d.identificacion());
            }
            case tarjeta -> {
                Card c0 = mp.getCard();
                if (c0 == null) throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No existe Card para este método"
                );
                CardDto d = dto.card();
                if (d == null) throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Falta datos de card"
                );
                c0.setNumeroTarjeta(d.numeroTarjeta());  
                c0.setTitular(d.titular());
                c0.setMes(d.mes());
                c0.setYear(d.year());
                c0.setTipo(d.tipo().charAt(0));
                c0.setRed(RedTarjeta.valueOf(d.red()));
            }
            case ewallet -> {
                Ewallet e0 = mp.getEwallet();
                if (e0 == null) throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No existe Ewallet para este método"
                );
                EwalletDto d = dto.ewallet();
                if (d == null) throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Falta datos de ewallet"
                );
                e0.setDireccion(d.direccion());            
                Criptocoin crypto = ccRepo
                    .findByAbreviacion(d.criptocoinAbreviacion().toUpperCase())
                    .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Criptomoneda no válida: " + d.criptocoinAbreviacion()
                    ));
                e0.setCriptocoin(crypto);
            }
        }

        return mpRepo.save(mp);
    }

    @Transactional
    public void deactivate(Integer id) {
        MetodoPago mp = mpRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Método no encontrado"));
        mp.setEstado(EstadoPago.inactivo);
    }

    public void delete(Integer id) {
        if (!mpRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Método no encontrado");
        }
        mpRepo.deleteById(id);
    }

  public PaymentMethodsResponse findByUserCorreo(String correo) {
        User user = userRepo.findByCorreo(correo)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Usuario no encontrado con correo " + correo
            ));
        List<MetodoPago> mps = mpRepo.findAllByUserId(user.getId());

        var cuentas = new ArrayList<BankAccountInfoDto>();
        var tarjetas = new ArrayList<CardInfoDto>();
        var wallets = new ArrayList<EwalletInfoDto>();

        for (MetodoPago mp : mps) {
            String estado = mp.getEstado().name();
            switch (mp.getTipo()) {
                case cuentabanco -> {
                    BankAccount ba = mp.getBankAccount();
                    if (ba != null) {
                        cuentas.add(new BankAccountInfoDto(
                            mp.getId(),
                            estado,
                            ba.getNombreBanco(),
                            ba.getNumeroCuenta(),
                            ba.getTipoCuenta().name(),
                            ba.getTitular(),
                            ba.getIdentificacion()
                        ));
                    }
                }
                case tarjeta -> {
                    Card c = mp.getCard();
                    if (c != null) {
                        tarjetas.add(new CardInfoDto(
                            mp.getId(),
                            estado,
                            c.getNumeroTarjeta(),
                            c.getTitular(),
                            c.getMes(),
                            c.getYear()
                        ));
                    }
                }
                case ewallet -> {
                    Ewallet e = mp.getEwallet();
                    if (e != null) {
                        wallets.add(new EwalletInfoDto(
                            mp.getId(),
                            estado,
                            e.getDireccion(),
                            e.getCriptocoin().getAbreviacion()
                        ));
                    }
                }
            }
        }

        return new PaymentMethodsResponse(cuentas, tarjetas, wallets);
    }
}
