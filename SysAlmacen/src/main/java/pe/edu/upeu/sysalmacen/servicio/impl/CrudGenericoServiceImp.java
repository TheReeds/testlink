package pe.edu.upeu.sysalmacen.servicio.impl;

import pe.edu.upeu.sysalmacen.excepciones.CustomResponse;
import pe.edu.upeu.sysalmacen.excepciones.ModelNotFoundException;
import pe.edu.upeu.sysalmacen.repositorio.ICrudGenericoRepository;
import pe.edu.upeu.sysalmacen.servicio.ICrudGenericoService;

import java.time.LocalDateTime;
import java.util.List;

public abstract class CrudGenericoServiceImp<E, K> implements ICrudGenericoService<E, K> {
    
    // Constantes para mensajes y respuestas
    private static final String ID_NOT_FOUND_MSG = "ID NOT FOUND: ";
    private static final String SUCCESS_MESSAGE = "true";
    private static final String SUCCESS_DETAILS = "Todo Ok";
    private static final int SUCCESS_STATUS_CODE = 200;
    
    protected abstract ICrudGenericoRepository<E, K> getRepo();

    @Override
    public E save(E entity) {
        return getRepo().save(entity);
    }

    @Override
    public E update(K id, E entity) {
        E existingEntity = getRepo().findById(id)
            .orElseThrow(() -> new ModelNotFoundException(ID_NOT_FOUND_MSG + id));
        return getRepo().save(entity);
    }

    @Override
    public List<E> findAll() {
        return getRepo().findAll();
    }

    @Override
    public E findById(K id) {
        return getRepo().findById(id)
            .orElseThrow(() -> new ModelNotFoundException(ID_NOT_FOUND_MSG + id));
    }

    @Override
    public CustomResponse delete(K id) {
        E entity = getRepo().findById(id)
            .orElseThrow(() -> new ModelNotFoundException(ID_NOT_FOUND_MSG + id));
        getRepo().deleteById(id);
        
        CustomResponse response = new CustomResponse();
        response.setStatusCode(SUCCESS_STATUS_CODE);
        response.setDatetime(LocalDateTime.now());
        response.setMessage(SUCCESS_MESSAGE);
        response.setDetails(SUCCESS_DETAILS);
        return response;
    }
}
