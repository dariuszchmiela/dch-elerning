"use client";

import { useState } from "react";

const [email, setEmail] = useState();
const [password, setPassword] = useState();
const [error, setError] = useState<string | null>(null);
const [success, setSuccess] = useState(false);

type Role = "STUDENT" | "INSTRUCTOR";
const [role, setRole] = useState<Role>("STUDENT");

async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
}

export default function RegisterPage() {

}

