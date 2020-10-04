package com.japa.cadastro.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.japa.cadastro.domain.Prato;

@Mapper(componentModel = "cdi")
public interface PratoMapper {	
	
	Prato toPrato(AdicionarPratoDTO dto);	
	
	void toPrato(AtualziarPratoDTO dto, @MappingTarget Prato prato);	
	
	PratoDTO toPratoDTO(Prato p);
	

}
