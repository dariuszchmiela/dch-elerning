"use client";

import {useEffect, useState} from "react";

type User = {
    id: number;
    email: string;
    role: string;
    createdAt: string;
};

export default function MePage() {
    const [user, setUser] = useState<User | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem("token");

        if (!token) {
            fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/users/me`, {
                headers: { Authorization: `Bearer ${token}` },
            })
            setError("No token — please log in");
            setLoading(false);
            return;
        }
    }, []);
}