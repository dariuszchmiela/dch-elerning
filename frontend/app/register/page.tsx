"use client";

import React, { useState } from "react";

type Role = "STUDENT" | "INSTRUCTOR";

export default function RegisterPage() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [role, setRole] = useState<Role>("STUDENT");
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e: React.SubmitEvent<HTMLFormElement>)  {
        e.preventDefault();
        setError(null);
        setLoading(true);

        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/users/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password, role }),
        });

        if (!response.ok) {
            const problem = await response.json();
            setError(problem.detail ?? "Registration failed");
            return;
        }

        setSuccess(true);
    }

    return (
        <form onSubmit={handleSubmit}>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" />
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Password" />
            <select value={role} onChange={(e) => setRole(e.target.value as Role)}>
                <option value="STUDENT">Student</option>
                <option value="INSTRUCTOR">Instructor</option>
            </select>
            {error && <p>{error}</p>}
            {success && <p>Zarejestrowano pomyślnie!</p>}
            <button type="submit">Register</button>
        </form>
    );
}