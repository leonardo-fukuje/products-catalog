package com.devsuperior.dscatalog.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;


@Service
public class ProductService {
	
	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(PageRequest pageRequest){
		Page<Product> list = repository.findAll(pageRequest);
		Page<ProductDTO> listDto = list.map(x -> new ProductDTO(x));

		return listDto;
	}

	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> obj = repository.findById(id);
		Product entity = obj.orElseThrow(() -> new EntityNotFoundException("Entity not found"));
		return new ProductDTO(entity, entity.getCategories());
	}
	
	@Transactional
	public ProductDTO insert(ProductDTO dto) {
		Product entity =  new Product();
		cpyDTOToEntity(dto, entity);
		entity = repository.save(entity);
		return new ProductDTO(entity);
	}


	@Transactional
	public ProductDTO update(Long id, ProductDTO dto) {
		try {
			Product entity = repository.getReferenceById(id);
			cpyDTOToEntity(dto, entity);
			entity = repository.save(entity);
			return new ProductDTO(entity);
		}
		catch(EntityNotFoundException e) {
			throw new ResourceNotFoundException("id not found");
		}
	}

	public void delete(Long id) {
		try {
		repository.deleteById(id);
	} catch(EmptyResultDataAccessException e) {
		throw new ResourceNotFoundException("id not found");
	} catch(DataIntegrityViolationException e) {
		throw new DatabaseException("Integrity violation");
	}
		}
	
	private void cpyDTOToEntity(ProductDTO dto, Product entity) {
		entity.setName(dto.getName());
		entity.setDate(dto.getDate());
		entity.setDescription(dto.getDescription());
		entity.setImgUrl(dto.getImgUrl());
		entity.setPrice(dto.getPrice());
		
		entity.getCategories().clear();
		for (CategoryDTO catDTO : dto.getCategories()) {
			Category category = categoryRepository.getReferenceById(catDTO.getId());
			entity.getCategories().add(category);
		}
	}
}
