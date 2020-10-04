package com.japa.cadastro.resources;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlow;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlows;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.japa.cadastro.domain.Prato;
import com.japa.cadastro.domain.Restaurante;
import com.japa.cadastro.dto.AdicionarPratoDTO;
import com.japa.cadastro.dto.AdicionarRestauranteDTO;
import com.japa.cadastro.dto.AtualizarRestauranteDTO;
import com.japa.cadastro.dto.AtualziarPratoDTO;
import com.japa.cadastro.dto.PratoMapper;
import com.japa.cadastro.dto.RestauranteDTO;
import com.japa.cadastro.dto.RestauranteMapper;

import infra.ConstraintViolationResponse;

@Path("/restaurantes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "restaurante")
@RolesAllowed("proprietario")
@SecurityScheme(securitySchemeName = "ifood_oauth", type = SecuritySchemeType.OAUTH2,
flows = @OAuthFlows(password = @OAuthFlow(tokenUrl = "http://localhost:8180/auth/realms/ifood/protocol/openid-connect/token")))
@SecurityRequirements(value = {@SecurityRequirement(name = "ifood_oauth", scopes = {})})
public class RestauranteResource {
	
	@Inject
	RestauranteMapper restauranteMapper;
	
	@Inject
	PratoMapper pratoMapper;

    @GET
    @Counted(name = "Quantidade buscas Restaurante")
    @SimplyTimed(name = "Tempo simples de busca")
    @Timed(name = "Tempo completo de busca")
    public List<RestauranteDTO> buscar() {
    	Stream<Restaurante> restaurantes = Restaurante.streamAll();
    	return restaurantes.map(r -> restauranteMapper.toRestauranteDTO(r)).collect(Collectors.toList());
    }
    
    @POST
    @Transactional
    @APIResponse(responseCode = "201", description = "Caso restaurante seja cadastrado com sucesso")
    @APIResponse(responseCode = "400", content = @Content(schema = @Schema(allOf = ConstraintViolationResponse.class)))
    public Response adicionar(@Valid AdicionarRestauranteDTO dto) {
    	Restaurante restaurante = restauranteMapper.toRestaurante(dto);
    	restaurante.persist();
    	return Response.status(Status.CREATED).build();
    }
    
    @PUT
    @Path("{id}")
    @Transactional
    public void atualizar(@PathParam("id") Long id, AtualizarRestauranteDTO  dto) {
    	Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(id);
    	if(restauranteOp.isEmpty()) {
    		throw new NotFoundException("Restaurante não existe");
    	}
    	Restaurante restaurante = restauranteOp.get();
    	
    	restauranteMapper.toRestaurante(dto,restaurante);
    	
    	restaurante.persist();
    }
    
    @DELETE
    @Path("{id}")
    @Transactional
    public void deletar(@PathParam("id") Long id) {
    	Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(id);
    	
    	restauranteOp.ifPresentOrElse(Restaurante::delete, () -> {
    		throw new NotFoundException("Restaurante não existe");
    	});    	
    }
    
    // pratos
    
    @GET
    @Path("{idRestaurante}/pratos")
    @Tag(name = "prato")
    public List<Prato> buscarPratos(@PathParam("idRestaurante") Long idRestaurante) {
    	Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);
    	if(restauranteOp.isEmpty()) {
    		throw new NotFoundException("Restaurante não existe");
    	}
    	return Prato.list("restaurante", restauranteOp.get());
    }
    
    @POST
    @Path("{idRestaurante}/pratos")
    @Transactional
    @Tag(name = "prato")
    public Response adicionarPrato(@PathParam("id") Long idRestaurante, AdicionarPratoDTO dto) {
    	Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);
    	if(restauranteOp.isEmpty()) {
    		throw new NotFoundException("Restaurante não existe");
    	}
    	
    	Prato prato = pratoMapper.toPrato(dto);    	
    	
    	prato.persist();
    	return Response.status(Status.CREATED).build();    	
    }
    
    
    
    @PUT
    @Path("{idRestaurante}/pratos/{id}")
    @Transactional
    @Tag(name = "prato")
    public void atualizar(@PathParam("idRestaurante") Long idRestaurante, @PathParam("id") Long id, AtualziarPratoDTO dto) {
    	Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);
    	if(restauranteOp.isEmpty()) {
    		throw new NotFoundException("Restaurante não existe");
    	}
    	
    	// No caso, id do prato vai ser unico, indep do0 restaurante...
    	Optional<Prato> pratoOp = Prato.findByIdOptional(idRestaurante);
    	if(pratoOp.isEmpty()) {
    		throw new NotFoundException("Prato não existe");
    	}
    	Prato prato = pratoOp.get();    	
    	pratoMapper.toPrato(dto, prato);
    	
    	prato.persist();
    }
    
    @DELETE
    @Path("{idRestaurante}/pratos/{id}")
    @Transactional
    @Tag(name = "prato")
    public void deletar(@PathParam("idRestaurante") Long idRestaurante, @PathParam("id") Long id) {
    	Optional<Restaurante> restaraunteOp = Restaurante.findByIdOptional(idRestaurante);
    	if(restaraunteOp.isEmpty()) {
    		throw new NotFoundException("Restaurante não existe");
    	}  
    	
    	Optional<Prato> pratoOp = Prato.findByIdOptional(id);
    	
    	pratoOp.ifPresentOrElse(Prato::delete, () -> {
    		throw new NotFoundException();
    	});
    }
    
}