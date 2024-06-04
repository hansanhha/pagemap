package com.bintage.pagemap.storage.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class WebArchiveProcessor {


}
