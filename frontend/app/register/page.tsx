"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

type Role = "STUDENT" | "INSTRUCTOR";

export default function RegisterPage() {
    const router = useRouter();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [role, setRole] = useState<Role>("STUDENT");
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e: React.SubmitEvent<HTMLFormElement>) {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
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

            router.push("/login");
        } finally {
            setLoading(false);
        }
    }

    return (
        <form onSubmit={handleSubmit} className="flex flex-col gap-4 max-w-sm mx-auto mt-20 p-6">
            <h1 className="text-xl font-bold">Create account</h1>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" className="border border-gray-300 rounded px-3 py-2" />
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Password" className="border border-gray-300 rounded px-3 py-2" />
            <select value={role} onChange={(e) => setRole(e.target.value as Role)} className="border border-gray-300 rounded px-3 py-2">
                <option value="STUDENT">Student</option>
                <option value="INSTRUCTOR">Instructor</option>
            </select>
            {error && <p className="text-red-600 text-sm">{error}</p>}
            <button type="submit" disabled={loading} className="bg-blue-600 text-white rounded px-4 py-2 hover:bg-blue-700 disabled:bg-gray-400">
                {loading ? "Registering..." : "Register"}
            </button>
            <p className="text-sm text-center">
                Already have an account? <Link href="/login" className="text-blue-600 hover:underline">Log in</Link>
            </p>
        </form>
    );
}