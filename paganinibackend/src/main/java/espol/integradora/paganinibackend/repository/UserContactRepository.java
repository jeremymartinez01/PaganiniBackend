package espol.integradora.paganinibackend.repository;

import espol.integradora.paganinibackend.Dto.ContactDto;
import espol.integradora.paganinibackend.model.UserContact;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserContactRepository extends JpaRepository<UserContact, Long> {

    boolean existsByOwner_IdAndContact_Id(Integer ownerId, Integer contactId);


    @Query("""
        select uc
        from UserContact uc
        join fetch uc.contact c
        where uc.owner.id = :ownerId
          and uc.status = espol.integradora.paganinibackend.model.UserContact.Status.ACCEPTED
        """)
    List<UserContact> findAcceptedByOwnerId(@Param("ownerId") Integer ownerId);

    Optional<UserContact> findByOwner_IdAndContact_Id(Integer ownerId, Integer contactId);

    @Modifying
    @Query("delete from UserContact uc where uc.owner.id = :ownerId and uc.contact.id = :contactId")
    int deleteByOwnerIdAndContactId(@Param("ownerId") Integer ownerId,
                                    @Param("contactId") Integer contactId);

    @Query("""
        select new espol.integradora.paganinibackend.Dto.ContactDto(
            u.nombre, u.apellido, u.correo, u.telefono
        )
        from UserContact uc
        join uc.contact u
        where uc.owner.id = :ownerId
        order by u.nombre asc, u.apellido asc
        """)
    List<ContactDto> findContactDtosByOwnerId(@Param("ownerId") Integer ownerId);

}
