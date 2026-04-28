package com.example.erudio.model.math;

import com.example.erudio.request.converters.NumberConverter;
import com.example.erudio.exception.UnsupportedMathOperationException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


public record SimpleMath() {

    public Double sum(Double numberOne, Double numberTwo) {
        return numberOne + numberTwo;
    }


    public Double subtraction(Double numberOne, Double numberTwo) {
        return numberOne - numberTwo;
    }

    public Double multiplication(Double numberOne, Double numberTwo) {
        return numberOne * numberTwo;
    }


    public Double division(Double numberOne, Double numberTwo) {
        return  numberOne / numberTwo;
    }


    public Double mean(Double numberOne, Double numberTwo) {
        return (numberOne+numberTwo)/2;
    }

    public Double squareRoot(Double number){
        return Math.sqrt(number);
    }

}

