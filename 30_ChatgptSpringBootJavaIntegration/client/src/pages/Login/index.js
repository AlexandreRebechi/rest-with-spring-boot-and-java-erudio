import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './styles.css';

import api from '../../services/api'

import logoImage from '../../assets/spring_icon.png'
import padLock from '../../assets/secure.png'

export default function Login() {

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const navigate = useNavigate();

    async function login(e) {
        e.preventDefault();

        const data = {
            username,
            password,
        };

        try {
            const response = await api.post('auth/signin', data);

            console.log('Resposta completa:', response);
            console.log('Status:', response.status);
            console.log('Data:', response.data);
            console.log('Token:', response.data?.token);
            console.log('AccessToken:', response.data?.accessToken);


            localStorage.setItem('username', username)
            localStorage.setItem('accessToken', response.data.accessToken)

            navigate('/books')
        } catch (error) {
            alert('Login failed! Try agains!')
        }

    };

    return (
        <div className="login-container">
            <section className='form'>
                <img src={logoImage} alt="Spring Boot Logo" />
                <form onSubmit={login}>
                    <h1>Access your Account</h1>
                    <input
                        placeholder="Username"
                        value={username}
                        onChange={e => setUsername(e.target.value)}
                    />
                    <input
                        type="password" placeholder="Password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                    />

                    <button className="button" type='submit'>Login</button>
                </form>
            </section>

            <img src={padLock} alt="Login" />


        </div>

    )
}