import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function Register() {
    const [nombre, setNombre] = useState('');
    const [correo, setCorreo] = useState('');
    const [contrasena, setContrasena] = useState('');
    const [confirm, setConfirm] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { register, login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        if (!nombre || !correo || !contrasena) {
            setError('Todos los campos son obligatorios');
            return;
        }
        if (contrasena !== confirm) {
            setError('Las contraseñas no coinciden');
            return;
        }

        setLoading(true);
        try {
            // Crear usuario como CLIENTE
            await register({ nombre, correo, contrasena, rol: 'CLIENTE' });
            // Hacer login automático
            const user = await login(nombre, contrasena);
            navigate(user.rol === 'ADMIN' ? '/admin/productos' : '/');
        } catch (err) {
            setError(err.message || 'Error al registrar');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="register-page">
            <div className="register-box">
                <h2>Crear cuenta</h2>
                {error && <div className="form-error">{error}</div>}
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Nombre de usuario</label>
                        <input className="form-control" value={nombre} onChange={e => setNombre(e.target.value)} required />
                    </div>
                    <div className="form-group">
                        <label>Correo</label>
                        <input className="form-control" type="email" value={correo} onChange={e => setCorreo(e.target.value)} required />
                    </div>
                    <div className="form-group">
                        <label>Contraseña</label>
                        <input className="form-control" type="password" value={contrasena} onChange={e => setContrasena(e.target.value)} required />
                    </div>
                    <div className="form-group">
                        <label>Confirmar contraseña</label>
                        <input className="form-control" type="password" value={confirm} onChange={e => setConfirm(e.target.value)} required />
                    </div>
                    <button className="btn btn-accent btn-full" type="submit" disabled={loading}>
                        {loading ? 'Registrando...' : 'Crear cuenta'}
                    </button>
                </form>
            </div>
        </div>
    );
}
