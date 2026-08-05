"use client";

import { useState } from "react";

const [email, setEmail] = useState();
const [password, setPassword] = useState();

type Role = "STUDENT" | "INSTRUCTOR";
const [role, setRole] = useState<Role>("STUDENT");

