package com.es.API_REST_SEGURA.service

import com.es.API_REST_SEGURA.dto.TareaDTO
import com.es.API_REST_SEGURA.dto.TareaRegisterDTO
import com.es.API_REST_SEGURA.error.exception.ForbiddenException
import com.es.API_REST_SEGURA.error.exception.NotFoundException
import com.es.API_REST_SEGURA.model.Estado
import com.es.API_REST_SEGURA.repository.TareaRepository
import com.es.API_REST_SEGURA.util.DtoMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class TareaService {

    @Autowired
    private lateinit var tareaRepository: TareaRepository

    private val dtoMapper = DtoMapper()

    fun getTareasByUsername(username: String, authentication: Authentication): List<TareaDTO> {
        var tareas = tareaRepository.getTareas(username.lowercase()).map { tarea ->
            dtoMapper.tareaEntityToDTO(tarea)
        }

        if (authentication.authorities.any {it.authority == "ROLE_ADMIN"}) {
            tareas = getAll()
        } else if (authentication.name == username) {
            if(tareas.isEmpty()){
                throw ForbiddenException("No hay tareas para $username")
            } else {
                return tareas
            }
        }

        return tareas
    }

    fun getAll(): List<TareaDTO> {
        val tareas = tareaRepository.getAll().map { tarea ->
            dtoMapper.tareaEntityToDTO(tarea)
        }
        return tareas
    }

    fun insertTarea(tarea: TareaRegisterDTO, authentication: Authentication): TareaDTO? {
        if (authentication.name == tarea.username || authentication.authorities.any {it.authority == "ROLE_ADMIN"}) {
            val tareaRegister = dtoMapper.tareaDTOToEntity(tarea)
            tareaRepository.save(tareaRegister)
            val tareaRegistrada = dtoMapper.tareaEntityToDTO(tareaRegister)
            return tareaRegistrada
        } else {
            throw ForbiddenException("No puede insertar ${tarea.titulo} para otro usuario.")
        }
    }

    fun deleteTareaByTitulo(titulo: String, authentication: Authentication) {
        val tarea = tareaRepository.getTareaByTitulo(titulo.lowercase())
        if (tarea != null) {
            if (authentication.name == tarea.username || authentication.authorities.any {it.authority == "ROLE_ADMIN"}) {
                return tareaRepository.delete(tarea)
            } else {
                throw ForbiddenException("No puedes eliminar tareas de otro usuario.")
            }
        } else {
            throw NotFoundException("No se ha encontrado ninguna tarea con este titulo: $titulo")
        }
    }

    fun cambiarEstadoTarea(titulo: String, estado: Estado, authentication: Authentication): TareaDTO? {
        val tarea = tareaRepository.getTareaByTitulo(titulo.lowercase())
        if (tarea != null) {
            if (authentication.name == tarea.username || authentication.authorities.any {it.authority == "ROLE_ADMIN"}) {
                tarea.estado = estado
                tareaRepository.save(tarea)
                val tareaActulizada = dtoMapper.tareaEntityToDTO(tarea)
                return tareaActulizada
            } else {
                throw ForbiddenException("No puedes modificar tareas de otro usuario.")
            }
        } else {
            throw NotFoundException("No se ha encontrado ninguna tarea con este titulo: $titulo")
        }
    }

}