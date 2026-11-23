/* Datos ficticios: esta vista no realiza solicitudes ni escrituras al servidor. */
(function () {
'use strict';
var productos = Array.from({length: 14}, function (_, i) { return {idProducto:i+1,nombre:['Camiseta de algodón','Caja de envío mediana','Bolsa reutilizable','Etiqueta adhesiva'][i%4]+' '+(i+1),tipo:i%2?'Embalaje':'Ropa',precio:12.5+i,stock:i%4===0?2:30+i,stockMinimo:5,activo:i!==4}; });
var usuarios=Array.from({length:13},function(_,i){return {idUsuario:i+1,nombre:['Lucía','Mateo','Andrea'][i%3],apellido:'Ejemplo',dni:String(90000001+i),rol:i===0?'ADMINISTRADOR':i%3?'USER':'PROVEEDOR',estado:i%4?'ACTIVO':'INNACTIVO'};});
var paquetes=Array.from({length:14},function(_,i){return {idPaquete:i+1,nPedido:'PED-DEMO-'+(100+i),nombreUsuario:'Lucía',apellidoUsuario:'Ejemplo',estado:['Enviado','Recibido','Cancelado'][i%3],totalUnidades:6,total:153,fechaCreacion:'2026-11-22T09:30:00',fechaCambioEstado:'2026-11-23T10:00:00',motivoCancelacion:i%3===2?'Solicitud del cliente':null,detalles:[{idDetalles:1,nombreProducto:'Camiseta de algodón',cantidad:6,precioUnitario:25.5}]};});
var movimientos=Array.from({length:14},function(_,i){return {idMovimiento:i+1,idProducto:i%3+1,nombreProducto:'Camiseta de algodón',tipo:['ENTRADA','SALIDA','AJUSTE','PAQUETE','DEVOLUCION'][i%5],cantidad:6,stockAnterior:20,stockNuevo:26,referencia:'Pedido PED-DEMO-100',usuarioDni:'90000001',fecha:'2026-11-23T09:00:00'};});
var auditoria=Array.from({length:12},function(_,i){return {idEvento:i+1,fecha:'2026-11-23T10:15:00',usuarioDni:'90000001',accion:i%2?'PRODUCTO_ACTUALIZADO':'PAQUETE_CREADO',entidad:i%2?'PRODUCTO':'PAQUETE',entidadId:i+1,detalle:'Operación de demostración'};});
window.HD_DEMO=async function(url,method){
 if(method!=='GET') throw new Error('Vista demo: no se guardan cambios.');
 var u=new URL(url,location.origin),q=u.searchParams,path=u.pathname,rows;
 if(path==='/miperfil/datos')return usuarios[0];
 if(path==='/api/productos/tipos')return ['Ropa','Embalaje'];
 if(path==='/api/productos/alertas')return productos.filter(function(p){return p.stock<=p.stockMinimo;});
 if(path==='/api/reportes/resumen')return {productosActivos:13,productosInactivos:1,productosSinStock:0,productosStockBajo:4,unidadesEnStock:346,valorInventario:6892.5,montoPaquetesNoCancelados:1530,usuariosPorEstado:{ACTIVO:9,INNACTIVO:4},paquetesPorEstado:{Enviado:5,Recibido:5,Cancelado:4},topProductos:[{nombre:'Camiseta de algodón',unidades:84},{nombre:'Caja mediana',unidades:42}]};
 if(/^\/api\/productos\/\d+$/.test(path))return productos.find(function(p){return p.idProducto===Number(path.split('/').pop());});
 if(/^\/api\/usuarios\/\d+$/.test(path))return usuarios.find(function(p){return p.idUsuario===Number(path.split('/').pop());});
 if(/^\/api\/paquetes\/\d+$/.test(path))return paquetes.find(function(p){return p.idPaquete===Number(path.split('/').pop());});
 if(path.includes('movimientos')) rows=movimientos; else if(path.includes('productos')) rows=productos; else if(path.includes('usuarios'))rows=usuarios; else if(path.includes('paquetes'))rows=paquetes; else rows=auditoria;
 rows=rows.filter(function(r){return Array.from(q.entries()).every(function(kv){var k=kv[0],v=kv[1];if(['pagina','tamanio','desde','hasta'].includes(k)||v==='')return true;if(k==='stockBajo')return v!=='true'||r.stock<=r.stockMinimo;if(k==='activo')return String(r.activo)===v;if(k==='busqueda')return (r.nombre+' '+r.apellido+' '+r.dni).toLowerCase().includes(v.toLowerCase());if(k==='usuario')return r.usuarioDni.includes(v);if(k==='pedido')return r.nPedido.includes(v);return String(r[k]).toLowerCase().includes(v.toLowerCase());});});
 var n=Number(q.get('pagina')||0),size=10;return {contenido:rows.slice(n*size,(n+1)*size),pagina:n,tamanio:size,totalElementos:rows.length,totalPaginas:Math.ceil(rows.length/size),ultima:(n+1)*size>=rows.length};
};
})();
