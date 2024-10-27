(ns com.gaumala.felec.examples)

(def example-contrib-datos {:razonSocial "Distribuidora de Suministros Nacional S.A."
                            :estab "002"
                            :ptoEmi "001"
                            :dirMatriz "Enrique Guerrero Portilla OE1-34 AV. Galo Plaza Lasso"
                            :dirEstablecimiento "Sebastián Moreno S/N Francisco García"
                            :contribuyenteEspecial "5368"
                            :obligadoContabilidad "SI"})

(def example-factura-content
  {:infoTributaria {:secuencial "000000001"
                    :razonSocial "Distribuidora de Suministros Nacional S.A."
                    :ptoEmi "001"
                    :ruc "1792146739001"
                    :claveAcceso "2410202401179214673900110020010000000010000000113"
                    :estab "002"
                    :dirMatriz "Enrique Guerrero Portilla OE1-34 AV. Galo Plaza Lasso"
                    :ambiente 1
                    :codDoc "01"}
   :infoFactura {:pagos [{:formaPago "01"
                          :total 230
                          :plazo 30
                          :unidadTiempo "dias"}]
                 :direccionComprador "salinas y santiago"
                 :identificacionComprador "1713328506001"
                 :obligadoContabilidad "SI"
                 :totalConImpuestos [{:codigo 2
                                      :codigoPorcentaje 4
                                      :baseImponible 200
                                      :valor 30}]
                 :razonSocialComprador "PRUEBAS SERVICIO RENTAS INTERNAS"
                 :contribuyenteEspecial "5368"
                 :totalDescuento 0
                 :tipoIdentificacionComprador "04"
                 :importeTotal "230"
                 :totalSinImpuestos 200.0
                 :dirEstablecimiento "Sebastián Moreno S/N Francisco García"
                 :fechaEmision "24/10/2024"
                 :propina 0}
   :detalles [{:codigoPrincipal "001"
               :descripcion "PRESTACION DE SERVICIOS PROFESIONALES"
               :cantidad 1, :precioUnitario 200
               :descuento 0
               :precioTotalSinImpuesto 200
               :impuestos [{:codigo 2
                            :codigoPorcentaje 4
                            :baseImponible 200
                            :valor 30
                            :tarifa 15}]}]})
(def example-factura-input
  {:infoTributaria {:secuencial "000000001"}
   :infoFactura {:fechaEmision "24/10/2024"
                 :tipoIdentificacionComprador "04"
                 :razonSocialComprador "PRUEBAS SERVICIO RENTAS INTERNAS"
                 :identificacionComprador "1713328506001"
                 :direccionComprador "salinas y santiago"
                 :totalSinImpuestos 200.0
                 :totalDescuento 0
                 :totalConImpuestos [{:codigo 2
                                      :codigoPorcentaje 4
                                      :baseImponible 200
                                      :valor 30}]
                 :propina 0
                 :importeTotal "230"
                 :pagos [{:formaPago "01"
                          :total 230
                          :plazo 30
                          :unidadTiempo "dias"}]}
   :detalles [{:codigoPrincipal "001"
               :descripcion "PRESTACION DE SERVICIOS PROFESIONALES"
               :cantidad 1
               :precioUnitario 200
               :descuento 0
               :precioTotalSinImpuesto 200
               :impuestos [{:codigo 2
                            :codigoPorcentaje 4
                            :baseImponible 200
                            :valor 30
                            :tarifa 15}]}]})

(def example-autorizacion-res
  {:claveAccesoConsultada "2410202401179214673900110020010000000010000000113"
   :numeroComprobantes "1"
   :autorizaciones [{:estado "AUTORIZADO"
                     :numeroAutorizacion "2410202401179214673900110020010000000010000000113"
                     :fechaAutorizacion "2024-10-24T13:03:17-05:00"
                     :ambiente "PRUEBAS"
                     :comprobante "<factura />"}]})

