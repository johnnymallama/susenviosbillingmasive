# SUSENVIOS BILLING MASIVE

Facturacion electronica de SUSENVIOS con FACTURE

## Version

1.0.0

## Lenguaje

JAVA 1.8

## Framework

Spring Boot 2.4.5
Spring Cache 2.4.5

## Integacion Externa

- **Facture**, empresa colombiana de facturacion electronica
- **XUE_SUSENVIO**, base de datos SQL SERVER, administracion por parte de SUSENVIOS

## Atributos de configuracion (application.properties)

- spring.datasource.url
- spring.datasource.username
- spring.datasource.password
- factureApi.host
- factureApi.url.login
- factureApi.url.sendDocument
- factureApi.user
- factureApi.pass

## Diagrama Componentes

## Diagrama MER

## Anexo DIAN

La integracion con Facture esta basada en el Anexo Tecnico de la DIAN 1.8 [Ver Aqui](https://www.dian.gov.co/impuestos/factura-electronica/Documents/Anexo-Tecnico-Resolucion-000012-09022021.pdf)