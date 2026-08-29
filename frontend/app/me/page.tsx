"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

type User = {
    id: number;
    email: string;
    role: string;
    createdAt: string;
};

export default function MePage() {
    const router = useRouter();
    const [user, setUser] = useState<User | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem("token");

        if (!token) {
            setError("No token — please log in");
            setLoading(false);
            return;
        }

        fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/users/me`, {
            headers: { Authorization: `Bearer ${token}` },
        })
            .then(async (response) => {
                if (!response.ok) {
                    const problem = await response.json();
                    setError(problem.detail ?? "Failed to load profile");
                    return;
                }
                setUser(await response.json());
            })
            .catch(() => setError("Network error"))
            .finally(() => setLoading(false));
    }, []);

    function handleLogout() {
        localStorage.removeItem("token");
        router.push("/login");
    }

    if (loading) {
        return <p className="mt-20 text-center">Loading...</p>;
    }

    if (error) {
        return <p className="mt-20 text-center text-red-600">{error}</p>;
    }

    return (
        <div className="flex flex-col gap-2 max-w-sm mx-auto mt-20 p-6">
            <h1 className="text-xl font-bold">My profile</h1>
            <p><span className="font-semibold">Email:</span> {user?.email}</p>
            <p><span className="font-semibold">Role:</span> {user?.role}</p>
            <p><span className="font-semibold">Member since:</span> {user?.createdAt}</p>
            <button onClick={handleLogout} className="bg-gray-200 rounded px-4 py-2 mt-4 hover:bg-gray-300">
                Log out
            </button>
        </div>
    );
}