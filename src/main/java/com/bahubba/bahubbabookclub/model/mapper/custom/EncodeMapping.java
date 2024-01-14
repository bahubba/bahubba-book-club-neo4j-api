package com.bahubba.bahubbabookclub.model.mapper.custom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.mapstruct.Qualifier;

/** Annotation for encoding a password in a mapper */
@Qualifier @Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface EncodeMapping {}
