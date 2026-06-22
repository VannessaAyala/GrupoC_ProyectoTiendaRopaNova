import React, { useState, useEffect } from 'react';
import { api } from '../../services/api';

export default function Pedidos() {
<<<<<<< Updated upstream
  const [pedidos, setPedidos] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadPedidos();
  }, []);

  const loadPedidos = () => {
    api.pedidos.getAll()
      .then(setPedidos)
      .finally(() => setLoading(false));
  };

  const handleCambiarEstado = async (id, nuevoEstado) => {
    try {
      await api.pedidos.updateEstado(id, nuevoEstado);
      loadPedidos();
    } catch (e) {
      alert("Error al actualizar el estado del pedido");
    }
  };
=======
    const [pedidos,  setPedidos]  = useState([]);
    const [loading,  setLoading]  = useState(true);
    const [filter,   setFilter]   = useState('TODOS');
    const [promedio, setPromedio] = useState(null);
    const [streamStatus, setStreamStatus] = useState('conectando');
    const [processingMessages, setProcessingMessages] = useState([]);
    const [processing, setProcessing] = useState(false);
    const { toast } = useToast();

    useEffect(() => {
        load();

        const source = new EventSource('/api/reactivo/pedidos/stream');
        source.onopen = () => setStreamStatus('conectado');
        source.onmessage = (event) => {
            const pedidoNuevo = JSON.parse(event.data);
            setPedidos(prev => {
                const existe = prev.some(p => p.id === pedidoNuevo.id);
                return existe
                    ? prev.map(p => (p.id === pedidoNuevo.id ? pedidoNuevo : p))
                    : [pedidoNuevo, ...prev];
            });
        };
        source.onerror = () => setStreamStatus('reconectando');

        const promedioSource = new EventSource('/api/reactivo/pedidos/promedio-stream');
        promedioSource.onmessage = (event) => setPromedio(Number(event.data));

        const processingSource = new EventSource('/api/reactivo/pedidos/procesamiento-stream');
        processingSource.onmessage = (event) => {
            setProcessingMessages(prev => [...prev.slice(-19), event.data]);
            if (event.data.includes('Flujo completado') || event.data.includes('Error en el flujo')) {
                setProcessing(false);
            }
        };

        return () => {
            source.close();
            promedioSource.close();
            processingSource.close();
        };
    }, []);
>>>>>>> Stashed changes

  if (loading) return <p>Cargando pedidos...</p>;

<<<<<<< Updated upstream
  return (
    <div>
      <div className="page-header">
        <h2>Gestión de Pedidos</h2>
      </div>
=======
    const handleProcesarLotes = async () => {
        setProcessingMessages([]);
        setProcessing(true);
        try {
            await api.pedidos.procesarLotes();
        } catch (err) {
            setProcessing(false);
            toast(err.message || 'Error iniciando el procesamiento', 'error');
        }
    };
>>>>>>> Stashed changes

      <table className="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Fecha</th>
            <th>Usuario</th>
            <th>Total</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {pedidos.map(p => (
            <tr key={p.id}>
              <td>#{p.id}</td>
              <td>{p.fecha}</td>
              <td>{p.usuario}</td>
              <td><strong>${parseFloat(p.total).toFixed(2)}</strong></td>
              <td>
                <span className="status-badge" style={{
                  background: p.estado === 'APROBADO' ? 'var(--success-color)' :
                              p.estado === 'RECHAZADO' ? 'var(--danger-color)' : '#f59e0b'
                }}>
                  {p.estado}
                </span>
<<<<<<< Updated upstream
              </td>
              <td>
                <div className="action-buttons">
                  {p.estado === 'PENDIENTE' && (
                    <>
                      <button
                        className="btn"
                        style={{ padding: '0.25rem 0.5rem', background: 'var(--success-color)' }}
                        onClick={() => handleCambiarEstado(p.id, 'APROBADO')}
                      >
                        Aprobar
                      </button>
                      <button
                        className="btn btn-danger"
                        style={{ padding: '0.25rem 0.5rem' }}
                        onClick={() => handleCambiarEstado(p.id, 'RECHAZADO')}
                      >
                        Rechazar
                      </button>
                    </>
                  )}
                  {p.estado !== 'PENDIENTE' && (
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Sin acciones</span>
                  )}
=======
                <span className={`badge ${streamStatus === 'conectado' ? 'badge-success' : 'badge-neutral'}`}>
                    SSE: {streamStatus}
                </span>
                <button
                    className="btn btn-outline btn-sm"
                    onClick={handleProcesarLotes}
                    disabled={processing}
                >
                    {processing ? 'Procesando...' : 'Ejecutar backpressure'}
                </button>
            </div>

            {processingMessages.length > 0 && (
                <div style={{
                    background: 'var(--card)',
                    border: '1px solid var(--border)',
                    borderRadius: '8px',
                    padding: '0.75rem 1rem',
                    marginBottom: '1rem',
                    fontFamily: 'monospace',
                    fontSize: '0.8rem'
                }}>
                    {processingMessages.map((message, index) => (
                        <div key={`${index}-${message}`}>{message}</div>
                    ))}
                </div>
            )}

            {/* Filtros */}
            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
                {FILTROS.map(f => (
                    <button
                        key={f}
                        className={`btn btn-sm ${filter === f ? 'btn-accent' : 'btn-outline'}`}
                        onClick={() => setFilter(f)}
                    >
                        {f === 'TODOS' ? 'Todos' : fmt.estado(f).label}
                        {f !== 'TODOS' && (
                            <span style={{ marginLeft: '4px', opacity: 0.7 }}>
                ({pedidos.filter(p => p.estado === f).length})
              </span>
                        )}
                    </button>
                ))}
            </div>

            {loading ? (
                <div className="loading-center"><div className="loading-ring" /></div>
            ) : (
                <div className="table-wrap">
                    <table className="data-table">
                        <thead>
                        <tr>
                            <th>Pedido</th>
                            <th>Fecha</th>
                            <th>Cliente</th>
                            <th>Total</th>
                            <th>Estado</th>
                            <th>Acciones</th>
                        </tr>
                        </thead>
                        <tbody>
                        {visibles.map(p => {
                            const { label, cls } = fmt.estado(p.estado);
                            const acciones = NEXT_ESTADOS[p.estado] || [];
                            return (
                                <tr key={p.id}>
                                    <td><strong>#{p.id}</strong></td>
                                    <td className="text-sm">{fmt.date(p.fecha)}</td>
                                    <td>{p.usuario}</td>
                                    <td><strong>{fmt.price(p.total)}</strong></td>
                                    <td><span className={`badge ${cls}`}>{label}</span></td>
                                    <td>
                                        {acciones.length > 0 ? (
                                            <div className="action-bar">
                                                {acciones.map(a => {
                                                    const { label: aLabel } = fmt.estado(a);
                                                    const cls = a === 'RECHAZADO' ? 'btn-danger'
                                                        : a === 'APROBADO' ? 'btn-success'
                                                            : 'btn-outline';
                                                    return (
                                                        <button
                                                            key={a}
                                                            className={`btn btn-sm ${cls}`}
                                                            onClick={() => handleEstado(p.id, a)}
                                                        >
                                                            {aLabel}
                                                        </button>
                                                    );
                                                })}
                                            </div>
                                        ) : (
                                            <span style={{ fontSize: '0.75rem', color: 'var(--muted)' }}>Finalizado</span>
                                        )}
                                    </td>
                                </tr>
                            );
                        })}
                        {visibles.length === 0 && (
                            <tr>
                                <td colSpan={6} style={{ textAlign: 'center', color: 'var(--muted)', padding: '2.5rem' }}>
                                    No hay pedidos con estado "{filter}"
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
>>>>>>> Stashed changes
                </div>
              </td>
            </tr>
          ))}
          {pedidos.length === 0 && (
            <tr><td colSpan="6" style={{ textAlign: 'center' }}>No hay pedidos registrados</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
