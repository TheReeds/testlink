package pe.edu.upeu.sysalmacen.servicio;

import pe.edu.upeu.sysalmacen.excepciones.CustomResponse;

import java.util.List;

public interface ICrudGenericoService<E, K> {
    E save(E entity);
    E update(K id, E entity);
    List<E> findAll();
    E findById(K id);
    CustomResponse delete(K id);
}