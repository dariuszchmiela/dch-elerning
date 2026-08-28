"use client";

import { useEffect, useState } from "react";

const [user, setUser] = useState<User | null>(null);
const [error, setError] = useState<string | null>(null);
const [loading, setLoading] = useState(true);

useEffect(() => {
    const token = localStorage.getItem("token");

    if (!token) {
        setError("Brak tokenu — zaloguj się");
        setLoading(false);
        return;
    }
}, []);

type User = {
    id: number;
    email: string;
    role: string;
    createdAt: string;
};

export default function MePage() {

}